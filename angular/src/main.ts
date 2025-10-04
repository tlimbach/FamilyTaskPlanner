import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { UsersComponent } from './app/users/users.component';

bootstrapApplication(UsersComponent, {
  providers: [
    provideHttpClient(withFetch())
  ]
}).catch(err => console.error(err));
