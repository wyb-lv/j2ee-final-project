import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Auth-gated routes depend on the JWT in localStorage, which exists only in the
  // browser. Render them client-side so reloading stays on the page instead of the
  // server-side guard (which sees no token) bouncing the user to /login.
  { path: 'admin', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: 'account', renderMode: RenderMode.Client },
  { path: 'checkout', renderMode: RenderMode.Client },
  {
    // Product pages are data-driven per id, so render them on demand
    // instead of trying to prerender every possible product.
    path: 'product/:id',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
