<template>
  <Toast />
  <div class="min-h-screen surface-ground text-900">
    <!-- Header -->
    <header class="surface-section border-bottom-1 surface-border">
      <div class="px-3 py-2">
        <Menubar :model="items" class="border-none surface-section">
          <template #start>
            <RouterLink
              to="/"
              class="no-underline flex align-items-center gap-2"
            >
              <i
                class="pi pi-shield text-primary text-xl"
                aria-hidden="true"
              ></i>
              <span class="text-xl font-bold tracking-tight text-900"
                >Secure Pay</span
              >
            </RouterLink>
          </template>

          <template #end>
            <div class="flex align-items-center gap-2">
              <RouterLink to="/" class="no-underline">
                <Button label="Home" link />
              </RouterLink>
              <RouterLink to="/payment" class="no-underline">
                <Button label="Payment" icon="pi pi-credit-card" link />
              </RouterLink>
              <RouterLink to="/payment-histories" class="no-underline">
                <Button label="Payment History" icon="pi pi-history" link />
              </RouterLink>
            </div>
          </template>
        </Menubar>
      </div>
    </header>

    <!-- Main -->
    <main class="px-3 py-4 md:px-5 md:py-6">
      <RouterView @paymentSuccess="onShowSuccessPayement" />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import Menubar from "primevue/menubar";
import Button from "primevue/button";
import Toast from "primevue/toast";
import { useToast } from "primevue/usetoast";

const toast = useToast();
const router = useRouter();

const items = [
  {
    label: "Home",
    icon: "pi pi-home",
    command: () => router.push("/"),
  },
  {
    label: "Payment",
    icon: "pi pi-credit-card",
    command: () => router.push("/payment"),
  },
  {
    label: "Payment History",
    icon: "pi pi-history",
    command: () => router.push("/payment-histories"),
  },
];

const onShowSuccessPayement = (msg = "") => {
  toast.add({
    severity: "success",
    summary: "Payement Success",
    detail: msg,
    life: 3000,
  });
};
</script>

<style scoped>
/* Optional: subtle hover for links when using Button link/text variants */
:deep(.p-button.p-button-link:hover),
:deep(.p-button.p-button-text:hover) {
  background: var(--surface-hover);
}
</style>
