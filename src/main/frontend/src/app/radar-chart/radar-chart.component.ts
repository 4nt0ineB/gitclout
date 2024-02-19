import {Component, ElementRef, HostListener, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import { ChartConfiguration, ChartData, ChartEvent, ChartType } from 'chart.js';
import {BaseChartDirective} from "ng2-charts";

@Component({
  selector: 'app-radar-chart',
  templateUrl: './radar-chart.component.html',
  styleUrl: './radar-chart.component.css'
})
export class RadarChartComponent  {
  @Input()
  contributor!: { labels: string[]; datasets: any[] };
  @ViewChild(BaseChartDirective)
  chart: BaseChartDirective | undefined;
  @ViewChild('chartCanvas') chartCanvas!: ElementRef;
  public radarChartData: ChartData<'radar'> = {
    labels: [],
    datasets: [],
  };
  public radarChartType: ChartType = 'radar';
  public radarChartOptions: ChartConfiguration['options'] = {
    plugins: {

      legend: {
        display: false,
        position: "bottom",
      },

    },
    scales: {
      r: {
        beginAtZero: true,
        suggestedMin: 0,
      },
    },

  };

  ngOnInit() {
    this.updateChartData();
  }

/*  ngOnChanges(changes: SimpleChanges): void {
    if (changes['contributor'] && this.contributor) {
      this.updateChartData();
    }
    console.log("update !");

  }*/

  config = {
    backgroundColor: 'rgba(255, 99, 132, 0.2)',
    borderColor: 'rgba(255, 99, 132, 1)',
    borderWidth: 1,
  };

  updateChartData(): void {
    this.radarChartData = {
      labels: this.contributor.labels,
      datasets: [...this.contributor.datasets, this.config],
    };
    if (this.chart) {
      this.chart.update();
    }
    console.log("update !");
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any): void {
    // Update chart dimensions on window resize
    if (this.chart) {
      const chartElement = this.chart.chart?.canvas;
      if (chartElement) {
        chartElement.width = chartElement.offsetWidth;
        chartElement.height = chartElement.offsetHeight;
      }
    }

    this.chart?.update();
  }
}
