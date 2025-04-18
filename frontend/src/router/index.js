import { createRouter, createWebHistory } from "vue-router";
import HomePage from "../views/HomePage.vue";
import LoginPage from "../views/LoginPage.vue";

const routes = [
    { path: "/", component: LoginPage },
    { path: "/home", component: HomePage },
    {
        path: '/user/:userId',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue')
    }

];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
