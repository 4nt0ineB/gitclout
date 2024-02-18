import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {RepositoryDetailComponent} from "./repository-detail/repository-detail.component";

const routes: Routes = [
  {path: ':repoId/tag/:tagSha1', component: RepositoryDetailComponent},
  {path: ':repoId', component: RepositoryDetailComponent },
  {path: '', component: HomeComponent},
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
