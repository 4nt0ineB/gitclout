import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {CommonModule} from "@angular/common";
import { HomeComponent } from './home/home.component';
import { RepositoryItemComponent } from './repository-item/repository-item.component';
import { RepositoryDetailComponent } from './repository-detail/repository-detail.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    RepositoryItemComponent, HomeComponent, RepositoryItemComponent, RepositoryDetailComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    CommonModule,
    ReactiveFormsModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
