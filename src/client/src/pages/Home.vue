<template>
  <Card>
    <template #content>
      <!-- Hero -->
      <div class="grid align-items-center gap-6">
        <!-- Left -->
        <div class="col-12 md:col-6">
          <div
            class="inline-flex align-items-center gap-2 mb-3 px-2 py-1 border-round bg-primary-50 text-primary border-1 surface-border"
          >
            <i class="pi pi-shield" aria-hidden="true"></i>
            <span class="font-medium">Enterprise-grade hardening</span>
          </div>

          <h1 class="text-4xl md:text-5xl font-bold line-height-2 m-0">
            Secure payments
          </h1>

          <p class="mt-3 text-lg">
            Application hardened with a strict Content Security Policy, input
            sanitization, and a clean, modern design.
          </p>

          <ul class="list-none p-0 m-0 mt-4">
            <li class="flex align-items-center gap-2 mb-2">
              <i class="pi pi-check-circle text-primary" aria-hidden="true"></i>
              <span class=""
                >Strict CSP (no
                <code class="px-1 border-round">unsafe-inline</code>)</span
              >
            </li>
            <li class="flex align-items-center gap-2 mb-2">
              <i class="pi pi-check-circle text-primary" aria-hidden="true"></i>
              <span class="">XSS-safe input sanitization</span>
            </li>
            <li class="flex align-items-center gap-2">
              <i class="pi pi-check-circle text-primary" aria-hidden="true"></i>
              <span class="">Accessible &amp; responsive UI</span>
            </li>
          </ul>

          <div class="flex flex-wrap gap-3 mt-5">
            <RouterLink
              to="/payment"
              aria-label="Go to Payment"
              class="no-underline"
            >
              <Button label="Go to Payment" icon="pi pi-lock" />
            </RouterLink>

            <a
              href="https://stripe.com/docs/payments"
              target="_blank"
              rel="noopener"
              aria-label="Learn more about Stripe payments"
              class="no-underline"
            >
              <Button label="Learn More" icon="pi pi-external-link" outlined />
            </a>
          </div>
        </div>

        <!-- Right -->
        <div
          class="col-12 md:col-4 border-solid border-3 border-round border-primary"
        >
          <Card class="w-full overflow-hidden">
            <template #content>
              <div class="relative">
                <div
                  class="flex align-items-center justify-content-center border-round-2xl font-bold"
                  style="height: 18rem"
                >
                  Secure&nbsp;Pay
                </div>

                <div class="flex gap-2">
                  <Tag value="CSP" severity="info" rounded />
                  <Tag value="Sanitized" severity="success" rounded />
                </div>
              </div>

              <p class="mt-3 mb-0 text-600">
                Navigate to the Payment page to try the validation &amp;
                sanitization demo.
              </p>
            </template>
          </Card>
        </div>
      </div>

      <!-- Feature Highlights -->
      <div class="grid">
        <div class="col-12">
          <Divider align="left">
            <span class="font-semibold">Why it’s solid</span>
          </Divider>
        </div>

        <div class="col-12 md:col-4">
          <Card class="h-full">
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-shield text-primary"></i>
                <span>Defense-in-depth</span>
              </div>
            </template>
            <template #content>
              <p class="m-0">
                CSP, sanitized inputs, and permission-scoped API calls reduce
                attack surface.
              </p>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-4">
          <Card class="h-full">
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-lock text-primary"></i>
                <span>Payments you can trust</span>
              </div>
            </template>
            <template #content>
              <p class="m-0">
                Clear flows, clear feedback, and strong affordances guide users
                through checkout.
              </p>
            </template>
          </Card>
        </div>
      </div>

      <!-- CTA -->
      <Card class="border-round-2xl shadow-1">
        <template #content>
          <div
            class="flex flex-column md:flex-row md:align-items-center md:justify-content-between gap-4"
          >
            <div>
              <h3 class="m-0">Ready to try it?</h3>
              <p class="m-0 mt-2 text-600">
                Head to the payment form to see validation and sanitization in
                action.
              </p>
            </div>
            <RouterLink to="/payment" class="no-underline">
              <Button
                size="large"
                label="Open Payment"
                icon="pi pi-arrow-right"
                iconPos="right"
              />
            </RouterLink>
          </div>
        </template>
      </Card>
    </template>
  </Card>
</template>

<script setup>
import Button from "primevue/button";
import Card from "primevue/card";
import Tag from "primevue/tag";
import Divider from "primevue/divider";

// If using this SFC standalone, ensure in main.ts you have:
// app.use(PrimeVue, { ripple: true })
// app.component('Button', Button)
// app.component('Card', Card)
// app.component('Tag', Tag)
// app.component('Divider', Divider)
// and PrimeFlex + PrimeIcons CSS are included globally.
</script>
