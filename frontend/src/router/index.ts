import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import {
  CampaignBeastsView,
  CampaignCartsView,
  CampaignCommerceView,
  CampaignDashboardView,
  CampaignDailyCycleView,
  CampaignHistoryView,
  CampaignInventoryView,
  CampaignRulesView,
  CampaignTravellersView,
} from '@/modules/campaign/views';

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
      path: '/campaigns/:campaignId/carts',
      name: 'campaign-carts',
      component: CampaignCartsView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/travellers',
      name: 'campaign-travellers',
      component: CampaignTravellersView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/inventory',
      name: 'campaign-inventory',
      component: CampaignInventoryView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/beasts',
      name: 'campaign-beasts',
      component: CampaignBeastsView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/commerce',
      name: 'campaign-commerce',
      component: CampaignCommerceView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/history',
      name: 'campaign-history',
      component: CampaignHistoryView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/rules',
      name: 'campaign-rules',
      component: CampaignRulesView,
      props: true,
    },
    {
      path: '/campaigns/:campaignId/day-cycle',
      name: 'campaign-day-cycle',
      component: CampaignDailyCycleView,
      props: true,
    },
  ],
});

export default router;
