import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-about',
  imports: [CommonModule, RouterLink],
  templateUrl: './about.html',
  styleUrl: './about.css',
})
export class About {
  readonly stats = [
    { value: '2009', label: 'Heating homes since' },
    { value: '12,000+', label: 'Systems installed' },
    { value: '70%', label: 'Average energy saved' },
    { value: '4.9★', label: 'Customer rating' },
  ];

  readonly values = [
    { icon: '☀', title: 'Clean energy first', text: 'Every system we sell is built to squeeze the most hot water from the least energy — sunshine before kilowatts.' },
    { icon: '🛡', title: 'Built to last', text: 'Stainless tanks, corrosion-proof collectors and components we would happily put in our own homes.' },
    { icon: '🤝', title: 'Honest advice', text: 'We size the right system for your home and climate — never the most expensive one on the shelf.' },
    { icon: '🔧', title: 'Real support', text: 'Certified technicians for installation, servicing and warranty — long after the sale.' },
  ];
}
