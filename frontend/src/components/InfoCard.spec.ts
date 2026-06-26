import { mount } from '@vue/test-utils';
import InfoCard from './InfoCard.vue';

describe('InfoCard', () => {
  it('renders label, value and help text', () => {
    const wrapper = mount(InfoCard, {
      props: {
        label: 'Carros',
        value: '12',
        helpText: 'Base de arquitectura preparada',
      },
    });

    expect(wrapper.text()).toContain('Carros');
    expect(wrapper.text()).toContain('12');
    expect(wrapper.text()).toContain('Base de arquitectura preparada');
  });
});
