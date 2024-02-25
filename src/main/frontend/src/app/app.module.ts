import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './component/app.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {CommonModule} from "@angular/common";
import { NgChartsModule } from 'ng2-charts';
import {HomeComponent} from "./component/home/home.component";
import {BarChartComponent} from "./component/bar-chart/bar-chart.component";
import {RepositoryItemComponent} from "./component/repository-item/repository-item.component";
import {RadarChartComponent} from "./component/radar-chart/radar-chart.component";
import {RepositoryDetailComponent} from "./component/repository-detail/repository-detail.component";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    BarChartComponent,
    RepositoryItemComponent,
    RadarChartComponent,
    HomeComponent,
    RepositoryItemComponent,
    RepositoryDetailComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    CommonModule,
    ReactiveFormsModule,
    NgChartsModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
