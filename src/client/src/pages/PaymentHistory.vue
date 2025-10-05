<template>
  <Card class="p-4 shadow-2 border-round-2xl">
    <template #content>
      <!-- Toolbar: Search + Filters + Actions -->
      <Toolbar class="mb-3 border-round-lg">
        <template #start>
          <div class="flex align-items-center gap-2">
            <span class="p-input-icon-left">
              <InputText
                v-model="search"
                placeholder="Search payments (order, intent, email)"
                class="w-20rem"
              />
            </span>

            <MultiSelect
              v-model="selectedStatuses"
              :options="statusOptions"
              placeholder="Status"
              display="chip"
              class="w-15rem"
            />

            <Dropdown
              v-model="selectedCurrency"
              :options="currencyOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="Currency"
              showClear
              class="w-10rem"
            />

            <Calendar
              v-model="dateRange"
              selectionMode="range"
              dateFormat="M d, yy"
              placeholder="Created date range"
              :manualInput="false"
              class="w-16rem"
            />
          </div>
        </template>
        <template #end>
          <div class="flex align-items-center gap-2">
            <Button
              label="Refresh"
              icon="pi pi-refresh"
              @click="load"
              :loading="isLoading"
            />
            <Button
              label="Export CSV"
              icon="pi pi-download"
              severity="secondary"
              @click="exportCSV"
            />
          </div>
        </template>
      </Toolbar>

      <DataTable
        ref="dt"
        :value="filteredPayments"
        dataKey="id"
        :loading="isLoading"
        :paginator="true"
        :rows="10"
        :rowsPerPageOptions="[10, 20, 50, 100]"
        responsiveLayout="scroll"
        class="payment-table"
        stripedRows
        sortMode="multiple"
        :emptyMessage="isLoading ? 'Loading payments…' : 'No payments found'"
      >
        <template #loading> Fetching payments… </template>
        <template #empty>No payment records found</template>

        <Column
          field="orderId"
          header="Order"
          sortable
          style="min-width: 12rem"
        >
          <template #body="{ data }">
            <div class="flex flex-column">
              <span class="font-medium">{{ data.orderId }}</span>
              <small class="text-500">{{ shortId(data.id) }}</small>
            </div>
          </template>
        </Column>

        <Column
          header="Amount"
          sortable
          sortField="amountCents"
          style="min-width: 10rem; text-align: right"
        >
          <template #body="{ data }">
            <div class="flex align-items-center justify-content-end gap-2">
              <span class="font-semibold">{{
                formatAmount(data.amountCents, data.currency)
              }}</span>
              <span class="text-500">{{ data.currency }}</span>
            </div>
          </template>
        </Column>

        <Column
          field="billingEmail"
          header="Billing Email"
          sortable
          style="min-width: 16rem"
        >
          <template #body="{ data }">
            <span>{{ data.billingEmail || "—" }}</span>
          </template>
        </Column>

        <Column
          field="status"
          header="Status"
          sortable
          style="min-width: 10rem"
        >
          <template #body="{ data }">
            <Tag
              :value="data.status"
              :severity="statusSeverity(data.status)"
              rounded
            />
          </template>
        </Column>

        <Column
          field="createdAt"
          header="Created"
          sortable
          style="min-width: 14rem"
        >
          <template #body="{ data }">
            <span>{{ formatDate(data.createdAt) }}</span>
          </template>
        </Column>

        <Column
          field="updatedAt"
          header="Updated"
          sortable
          style="min-width: 14rem"
        >
          <template #body="{ data }">
            <span>{{ formatDate(data.updatedAt) }}</span>
          </template>
        </Column>

        <Column header="Error" style="min-width: 10rem">
          <template #body="{ data }">
            <Button
              v-if="data.lastError"
              icon="pi pi-exclamation-triangle"
              severity="danger"
              text
              @click="openError(data)"
              v-tooltip.top="'View last error'"
            />
            <span v-else class="text-500">—</span>
          </template>
        </Column>
      </DataTable>

      <Dialog
        v-model:visible="errorDialogVisible"
        modal
        :header="`Last Error — Order ${selected?.orderId ?? ''}`"
        :style="{ width: '40rem' }"
      >
        <pre class="white-space-pre-wrap">{{ selected?.lastError }}</pre>
        <template #footer>
          <Button
            label="Close"
            icon="pi pi-check"
            @click="errorDialogVisible = false"
            autoFocus
          />
        </template>
      </Dialog>

      <div
        v-if="error"
        class="mt-3 p-3 border-1 surface-border border-round text-red-500"
      >
        <i class="pi pi-times-circle mr-2" /> {{ error }}
      </div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";

// PrimeVue components
import Card from "primevue/card";
import Toolbar from "primevue/toolbar";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Dropdown from "primevue/dropdown";
import MultiSelect from "primevue/multiselect";
import Calendar from "primevue/calendar";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Tag from "primevue/tag";
import Dialog from "primevue/dialog";
import Tooltip from "primevue/tooltip";

// Register tooltip directive locally
defineOptions({
  directives: { tooltip: Tooltip },
});

// --- Types ---
interface Payment {
  id: string;
  orderId: string;
  paymentIntentId?: string | null;
  amountCents: number;
  currency: string;
  billingEmail?: string | null;
  status: "PENDING" | "SUCCEEDED" | "FAILED" | string;
  lastError?: string | null;
  createdAt: string; // ISO date from backend
  updatedAt: string; // ISO date from backend
}

// --- API config ---
// If your controller has a class-level @RequestMapping("/api/payments"), change the path below accordingly.
const GET_ALL_ENDPOINT = "/get-all-payments";

// --- State ---
const payments = ref<Payment[]>([]);
const isLoading = ref(false);
const error = ref("");
const dt = ref();

// Filters
const search = ref("");
const selectedStatuses = ref<string[]>([]);
const selectedCurrency = ref<string | null>(null);
const dateRange = ref<[Date, Date] | null>(null);

const statusOptions = ["PENDING", "SUCCEEDED", "FAILED"];
const currencyOptions = computed(() => {
  const set = new Set(payments.value.map((p) => p.currency).filter(Boolean));
  return Array.from(set).map((c) => ({ label: c, value: c }));
});

// --- Fetch ---
async function load() {
  isLoading.value = true;
  error.value = "";
  try {
    const res = await fetch(`${GET_ALL_ENDPOINT}`, {
      method: "GET",
      headers: { Accept: "application/json" },
      cache: "no-store",
    });

    if (!res.ok) {
      let msg = "";
      try {
        msg = await res.text();
      } catch {}
      throw new Error(msg || `Request failed with status ${res.status}`);
    }

    const data = await res.json();
    payments.value = Array.isArray(data) ? data : [];
  } catch (e: any) {
    error.value = e?.message ?? "Failed to load payments.";
  } finally {
    isLoading.value = false;
  }
}

onMounted(load);

// --- Computed filtering ---
const filteredPayments = computed(() => {
  const q = search.value.trim().toLowerCase();
  const [start, end] = normalizeRange(dateRange.value);

  return payments.value.filter((p) => {
    const inSearch =
      !q ||
      [p.orderId, p.paymentIntentId, p.billingEmail, p.id]
        .filter(Boolean)
        .some((v) => String(v).toLowerCase().includes(q));

    const inStatus =
      !selectedStatuses.value.length ||
      selectedStatuses.value.includes(p.status);
    const inCurrency =
      !selectedCurrency.value || p.currency === selectedCurrency.value;

    const created = new Date(p.createdAt);
    const inDate = !start || !end || (created >= start && created <= end);

    return inSearch && inStatus && inCurrency && inDate;
  });
});

function normalizeRange(
  range: [Date, Date] | null
): [Date | null, Date | null] {
  if (!range || !range[0] || !range[1]) return [null, null];
  const start = new Date(range[0]);
  start.setHours(0, 0, 0, 0);
  const end = new Date(range[1]);
  end.setHours(23, 59, 59, 999);
  return [start, end];
}

// --- Helpers ---
function formatAmount(cents: number, currency: string) {
  try {
    return new Intl.NumberFormat(undefined, {
      style: "currency",
      currency,
    }).format(cents / 100);
  } catch {
    // Fallback if currency code is unexpected
    return `${(cents / 100).toFixed(2)} ${currency}`;
  }
}

function formatDate(iso: string) {
  const d = new Date(iso);
  return isNaN(d.getTime()) ? iso : d.toLocaleString();
}

function statusSeverity(status: string) {
  switch (status?.toUpperCase()) {
    case "SUCCEEDED":
      return "success";
    case "PENDING":
      return "warning";
    case "FAILED":
      return "danger";
    default:
      return "secondary";
  }
}

function shortId(id: string) {
  return id ? `${id.slice(0, 8)}…${id.slice(-4)}` : "";
}

function exportCSV() {
  (dt.value as any)?.exportCSV?.();
}

// Error dialog
const errorDialogVisible = ref(false);
const selected = ref<Payment | null>(null);

function openError(row: Payment) {
  selected.value = row;
  errorDialogVisible.value = true;
}
</script>

<style scoped>
.payment-table :deep(.p-tag) {
  text-transform: uppercase;
}
</style>
