import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import { CampaignDashboardView, CampaignRulesView } from '@/modules/campaign/views';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    {
      path: '/campaigns/:campaignId/dashboard',
      name: 'campaign-dashboard',
      component: CampaignDashboardView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/rules',
      name: 'campaign-rules',
      component: CampaignRulesView,
      props: true,
    },
  ],
});

export default router;
