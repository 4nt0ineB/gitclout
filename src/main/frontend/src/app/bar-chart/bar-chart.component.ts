import {Component, ElementRef, HostListener, Input, OnChanges, signal, SimpleChanges, ViewChild} from '@angular/core';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

import DataLabelsPlugin from 'chartjs-plugin-datalabels';

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.css'],
})
export class BarChartComponent implements OnChanges {
  @ViewChild(BaseChartDirective)
  chart: BaseChartDirective | undefined;
  @ViewChild('chartCanvas') chartCanvas!: ElementRef;
  @Input()
  transformedData!: { labels: string[]; datasets: any[] };

  public barChartType: ChartType = 'bar';
  public barChartPlugins = [DataLabelsPlugin];
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [],
  };

  public barChartOptions: ChartConfiguration['options'] = {
    maintainAspectRatio: false,
    aspectRatio: 0.8,
    responsive: true,
    scales: {
      x: { stacked: true },
      y: {
        type: 'logarithmic',
        title: {
          display: true,
          text: 'Number of Lines',
        },
      },
    },
    plugins: {
      title: {
        display: true,
        text: 'Contributions by Collaborator and Type',
        font: {
          size: 16,
        },
      },
      datalabels: {
        display: false,
        anchor: 'start',
        align: 'end',
      },
    },
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['transformedData'] && this.transformedData) {
      this.updateChartData();
    }
  }

  updateChartData(): void {
    this.barChartData = {
      labels: this.transformedData.labels,
      datasets: this.transformedData.datasets,
    };
    if (this.chart) {
      this.chart.update();
    }
  }

  ngOnInit(): void {
    this.barChartData = {
      labels: this.transformedData.labels,
      datasets: this.transformedData.datasets
    };
    if (this.chart) {
      this.chart.update();
    }
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
