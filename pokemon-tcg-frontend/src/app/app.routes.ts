import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'deck-builder',
    loadComponent: () =>
      import('./features/deck-builder/deck-builder.component').then(
        (component) => component.DeckBuilderComponent,
      ),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'deck-builder',
  },
];
