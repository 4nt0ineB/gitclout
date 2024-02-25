import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {RepositoryDetailComponent} from "./component/repository-detail/repository-detail.component";
import {HomeComponent} from "./component/home/home.component";

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
