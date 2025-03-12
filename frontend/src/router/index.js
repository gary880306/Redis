import { createRouter, createWebHistory } from "vue-router";
import HomePage from "../views/HomePage.vue";
import LoginPage from "../views/LoginPage.vue";

const routes = [
    { path: "/", component: LoginPage },
    { path: "/home", component: HomePage },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
