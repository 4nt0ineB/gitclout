import {Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import { ChartConfiguration, ChartData, ChartEvent, ChartType } from 'chart.js';
import {BaseChartDirective} from "ng2-charts";

@Component({
  selector: 'app-radar-chart',
  templateUrl: './radar-chart.component.html',
  styleUrl: './radar-chart.component.css'
})
export class RadarChartComponent implements OnChanges {
  @Input()
  contributor!: { labels: string[]; datasets: any[] };
  @ViewChild(BaseChartDirective)
  chart: BaseChartDirective | undefined;
  @ViewChild('chartCanvas') chartCanvas!: ElementRef;

  public radarChartOptions: ChartConfiguration['options'] = {
    plugins: {
      title: {
        display: true,
        text: `Contributions of none`,
        font: {
          size: 16,
        },
      },
      legend: {
        display: false,
        position: "bottom",
      },
      tooltip: {
        enabled: true,
      },
    },
    scales: {
      r: {
        beginAtZero: true,
        suggestedMin: 0,
      },
    },

  };

  public radarChartData: ChartData<'radar'> = {
    labels: [],
    datasets: [],
  };
  public radarChartType: ChartType = 'radar';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['contributor'] && this.contributor) {
      this.updateChartData();
    }
  }

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
  }
}
