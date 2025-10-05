<script setup>
import { onMounted, reactive, ref, computed } from "vue";
import { loadStripe } from "@stripe/stripe-js";
import { sanitizeString } from "../composables/useSanitize"; // amount handled by InputNumber
import { useRouter } from "vue-router";

// PrimeVue components
import Card from "primevue/card";
import InputText from "primevue/inputtext";
import InputNumber from "primevue/inputnumber";
import Button from "primevue/button";
import Message from "primevue/message";
import FloatLabel from "primevue/floatlabel";

const form = reactive({ orderId: "", email: "" });
const router = useRouter();
const amount = ref(0);
const busy = ref(false);
const message = ref("");
const cardError = ref("");
const stripeReady = ref(false);
const emit = defineEmits(["paymentSuccess"]);

const errors = reactive({ orderId: "", email: "", amount: "" });

let stripe = null;
let elements = null;
let card = null;

const messageSeverity = computed(() => {
  const m = message.value.toLowerCase();
  return m.includes("success") || m.includes("succeed") ? "success" : "error";
});

function sanitizeOrderId() {
  form.orderId = sanitizeString(form.orderId).slice(0, 64);
}
function sanitizeEmail() {
  form.email = sanitizeString(form.email).slice(0, 254);
}

function validate() {
  const errs = {};
  if (!form.orderId || form.orderId.length < 3) {
    errs.orderId = "Order ID is required (min 3 chars).";
  }
  if (form.email) {
    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    if (!emailRe.test(form.email)) errs.email = "Please enter a valid email.";
  }
  if (!Number.isFinite(amount.value) || amount.value <= 0) {
    errs.amount = "Amount must be a positive number.";
  }
  return errs;
}

// ---- API helpers (wired to your Spring controllers) ----
async function getPublishableKey() {
  const res = await fetch("/api/config");
  if (!res.ok) throw new Error("Failed to load config");
  const json = await res.json();
  if (!json.publishableKey) throw new Error("Missing publishable key");
  return json.publishableKey;
}

async function createIntent({ orderId, amountCents, currency, email }) {
  const res = await fetch("/api/payments/create-intent", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderId, amountCents, currency, email }),
  });
  if (!res.ok) {
    throw new Error(
      (await res.text().catch(() => "")) || "Failed to create PaymentIntent"
    );
  }
  return res.json(); // { clientSecret, paymentIntentId, paymentId }
}

async function recordResult({ paymentIntentId, status, error }) {
  const res = await fetch("/api/payments/record-result", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ paymentIntentId, status, error }),
  });
  if (!res.ok) {
    // non-fatal for user flow
    throw new Error(
      (await res.text().catch(() => "")) || "Failed to record payment result"
    );
  }
  return res.json(); // { ok: true }
}

// ---- Submit flow ----
async function onSubmit() {
  message.value = "";
  cardError.value = "";

  const v = validate();
  errors.orderId = v.orderId || "";
  errors.email = v.email || "";
  errors.amount = v.amount || "";
  if (Object.keys(v).length) return;

  if (!stripe || !card) {
    message.value = "Stripe is not ready. Please try again.";
    return;
  }

  busy.value = true;
  try {
    const amountCents = Math.round(Number(amount.value) * 100);
    const { clientSecret, paymentIntentId } = await createIntent({
      orderId: form.orderId,
      amountCents,
      currency: "usd",
      email: form.email || null,
    });

    const { paymentIntent, error } = await stripe.confirmCardPayment(
      clientSecret,
      {
        payment_method: {
          card,
          billing_details: { email: form.email || undefined },
        },
      }
    );

    const status = paymentIntent?.status || (error ? "failed" : "processing");
    const errMsg =
      error?.message || paymentIntent?.last_payment_error?.message || null;

    try {
      await recordResult({ paymentIntentId, status, error: errMsg });
    } catch (persistErr) {
      console.warn(persistErr);
    }

    if (status?.toLowerCase().includes("succeeded")) {
      message.value = "Payment success! 🎉";
      amount.value = 0;
      emit("paymentSuccess", message.value);
      await router.push("/payment-histories");
      return;
    } else if (status?.toLowerCase().includes("requires")) {
      message.value = "Additional action required. Please follow the prompts.";
    } else {
      message.value = errMsg || "Payment failed. Please try a different card.";
      cardError.value = errMsg || "";
    }
  } catch (e) {
    console.error(e);
    message.value = "Unexpected error. Please try again.";
  } finally {
    busy.value = false;
  }
}

// ---- Stripe setup ----
onMounted(async () => {
  try {
    const pk = await getPublishableKey();
    stripe = await loadStripe(pk);
    if (!stripe) throw new Error("Stripe failed to load");
    elements = stripe.elements();
    card = elements.create("card", { hidePostalCode: true });
    card.on("change", (evt) => (cardError.value = evt.error?.message || ""));
    card.mount("#card-element");
    stripeReady.value = true;
  } catch (e) {
    console.error(e);
    stripeReady.value = false;
    message.value = "Stripe not initialized. Check server config.";
  }
});
</script>

<template>
  <section class="flex justify-content-center p-3">
    <div class="w-full md:w-8 lg:w-6">
      <Card class="surface-card border-round shadow-1">
        <template #title>Payment</template>
        <template #subtitle
          >Secure payment with validated & sanitized inputs.</template
        >

        <template #content>
          <form
            class="p-fluid grid formgrid"
            @submit.prevent="onSubmit"
            novalidate
          >
            <!-- Order ID -->
            <div class="field col-12 mt-3 mb-3">
              <FloatLabel class="w-full">
                <InputText
                  class="w-full"
                  id="orderId"
                  v-model.trim="form.orderId"
                  @input="sanitizeOrderId"
                  :disabled="busy"
                  :class="{ 'p-invalid': !!errors.orderId }"
                  autocomplete="off"
                  inputmode="text"
                />
                <label for="orderId">Order ID</label>
              </FloatLabel>
              <Message v-if="errors.orderId" severity="error" class="mt-2">{{
                errors.orderId
              }}</Message>
            </div>

            <!-- Email -->
            <div class="field col-12 mt-3 mb-3">
              <FloatLabel>
                <InputText
                  class="w-full"
                  id="email"
                  v-model.trim="form.email"
                  @input="sanitizeEmail"
                  :disabled="busy"
                  :class="{ 'p-invalid': !!errors.email }"
                  type="email"
                  inputmode="email"
                  autocomplete="email"
                />
                <label for="email">Email (for receipt)</label>
              </FloatLabel>
              <Message v-if="errors.email" severity="error" class="mt-2">{{
                errors.email
              }}</Message>
            </div>

            <!-- Amount -->
            <div class="field col-12 mt-3 mb-3">
              <label for="amount" class="block text-color-secondary mb-2"
                >Amount (USD)</label
              >
              <InputNumber
                class="w-full"
                id="amount"
                v-model="amount"
                :disabled="busy"
                mode="currency"
                currency="USD"
                locale="en-US"
                :minFractionDigits="2"
                :maxFractionDigits="2"
                :class="{ 'p-invalid': !!errors.amount }"
                placeholder="10.00"
              />
              <Message v-if="errors.amount" severity="error" class="mt-2">{{
                errors.amount
              }}</Message>
            </div>

            <!-- Stripe Card -->
            <div class="field col-12 mt-3 mb-3">
              <label class="block text-color-secondary mb-2">Card</label>
              <div
                id="card-element"
                class="border-1 surface-border border-round p-3 surface-0"
                :class="{ 'border-red-400': !!cardError }"
              ></div>
              <Message v-if="cardError" severity="error" class="mt-2">{{
                cardError
              }}</Message>
              <Message v-if="!stripeReady" class="text-600 mt-2" severity="info"
                >Initializing Stripe…</Message
              >
            </div>

            <!-- Submit -->
            <div class="col-12 mt-2 mt-2 mb-3">
              <Button
                type="submit"
                icon="pi pi-credit-card"
                :label="busy ? 'Processing…' : 'Pay'"
                :loading="busy"
                :disabled="!stripeReady"
                class="w-full"
              />
            </div>
          </form>

          <div class="mt-1" v-if="message">
            <Message :severity="messageSeverity" :closable="true">{{
              message
            }}</Message>
          </div>
        </template>
      </Card>
    </div>
  </section>
</template>

<style scoped>
/* Make the Stripe card match PrimeVue input height & rounding */
#card-element {
  min-height: 44px;
}
</style>
