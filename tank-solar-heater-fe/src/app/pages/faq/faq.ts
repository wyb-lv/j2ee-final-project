import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface FaqItem {
  q: string;
  a: string;
  group: string;
}

@Component({
  selector: 'app-faq',
  imports: [CommonModule, RouterLink],
  templateUrl: './faq.html',
  styleUrl: './faq.css',
})
export class Faq {
  readonly open = signal<number | null>(0);

  toggle(i: number): void {
    this.open.update((cur) => (cur === i ? null : i));
  }

  readonly items: FaqItem[] = [
    {
      group: 'Solar heaters',
      q: 'How does a solar water heater work?',
      a: 'A roof-mounted collector absorbs sunlight and heats a fluid that transfers warmth to the water in your storage tank. On sunny days the sun does almost all the work; on cloudy days a backup element or heat pump tops it up so you never run cold.',
    },
    {
      group: 'Solar heaters',
      q: 'Will a solar heater work in winter or on cloudy days?',
      a: 'Yes. Our evacuated-tube collectors still capture heat in low light and freezing temperatures, and every solar system ships with an electric or heat-pump backup that keeps water hot whenever the sun isn\'t enough.',
    },
    {
      group: 'Tank heaters',
      q: 'What size storage tank do I need?',
      a: 'As a rule of thumb: 100–150L for 1–2 people, 200–300L for a family of 3–4, and 400L+ for larger households. Our team sizes the tank to your usage during the free consultation so you never run out — or pay to heat water you don\'t use.',
    },
    {
      group: 'Tank heaters',
      q: 'How long does the hot water last?',
      a: 'An insulated stainless tank holds heat for many hours, so a full tank comfortably covers back-to-back showers. Smart controllers also re-heat on a schedule so the tank is full when you need it most.',
    },
    {
      group: 'Savings',
      q: 'How much can I really save?',
      a: 'Water heating is typically 15–25% of a home\'s energy bill. Customers switching from a standard electric heater to a solar or heat-pump system save up to 70% on water-heating costs — most systems pay for themselves within a few years.',
    },
    {
      group: 'Installation',
      q: 'Do you handle installation?',
      a: 'Absolutely. Certified technicians manage mounting, plumbing, electrical and removal of your old unit — usually within a single day. Installation is included on most systems.',
    },
    {
      group: 'Installation',
      q: 'Can I switch from an old electric or gas heater?',
      a: 'In almost every case, yes. We assess your existing setup and recommend a solar, tank or hybrid system that fits your plumbing and climate, then handle the full changeover for you.',
    },
    {
      group: 'Warranty',
      q: 'What warranty do your heaters come with?',
      a: 'Tanks and solar collectors carry a 10-year warranty, with 2–5 years on electronic components depending on the model. Every install includes ongoing servicing and warranty support from our own technicians.',
    },
    {
      group: 'Orders',
      q: 'How do delivery and payment work?',
      a: 'Add a system to your cart and check out online. We confirm sizing, schedule delivery and installation, and offer flexible payment options. Prices shown already include any active discounts.',
    },
  ];
}
