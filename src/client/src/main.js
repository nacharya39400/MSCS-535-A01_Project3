import { createApp } from "vue";
import { createRouter, createWebHistory } from "vue-router";
import App from "./App.vue";
import Home from "./pages/Home.vue";
import Payment from "./pages/Payment.vue";
import "/node_modules/primeflex/primeflex.css";
import "primeflex/themes/primeone-dark.css";
import PrimeVue from "primevue/config";
import Aura from "@primeuix/themes/aura";
import "./style.css";
import "primeicons/primeicons.css";
import ToastService from "primevue/toastservice";
import PaymentHistory from "./pages/PaymentHistory.vue";

const routes = [
  { path: "/", name: "home", component: Home },
  { path: "/payment", name: "payment", component: Payment },
  {
    path: "/payment-histories",
    name: "PaymentHistory",
    component: PaymentHistory,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

const app = createApp(App);
app.use(ToastService);
app.use(PrimeVue, {
  // Default theme configuration
  theme: {
    preset: Aura,
    options: {
      prefix: "p",
      darkModeSelector: "system",
      cssLayer: false,
    },
  },
});
app.use(router).mount("#app");
