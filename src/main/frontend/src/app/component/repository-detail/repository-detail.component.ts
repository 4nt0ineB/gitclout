import {Component, HostListener, signal} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {RepositoryDetails, Tag} from "../../model/repositoryDetails";
import {RepositoryService} from "../../service/repository.service";
import {Utils} from "../../utils";
@Component({
  selector: 'app-repository-detail',
  templateUrl: './repository-detail.component.html',
  styleUrls: ['./repository-detail.component.css']
})
export class RepositoryDetailComponent {
  tagSha1!: string;
  repository!: RepositoryDetails;
  protected readonly Utils = Utils;
  byCatagory: boolean = false;
  users: string[] = [];
  sorting = '';


  chartData: { labels: string[], datasets: any[] } = {
    labels: [],
    datasets: []
  }

  chartDataCategory : { labels: string[], datasets: any[] } = {
    labels: [],
    datasets: []
  }

  constructor(private route: ActivatedRoute, private router: Router, private repositoryService: RepositoryService) { }

  currentLabels() {
    let data = this.byCatagory ? this.chartData : this.chartDataCategory;
    return data.datasets.map(value => value.label).sort();
  }

  onWheel(event: WheelEvent) {
    event.preventDefault(); // Prevent default vertical scrolling
    const delta = Math.max(-1, Math.min(1, (event.deltaY || -event.detail)));
    const container = event.currentTarget as HTMLElement;
    container.scrollLeft += delta * 60; // Adjust scroll speed as needed
  }

  choseSorting(newSortingLabel: string) {
    let data = this.byCatagory ? this.chartData : this.chartDataCategory;
    let labelData = data.datasets.find(obj => obj.label === newSortingLabel);
    console.log("----");
    console.log(data);
    if (labelData) {
      let lines: number[] = labelData.data;
      let permutation = lines.map((value, i) => ({ i, value }))
        .sort((a, b) => a.value - b.value)
        .map(tmp => tmp.i);
      data.datasets.forEach(value => {
        if (value.data) {
          value.data = this.organizeArray(value.data, permutation);
        }
      });
      data.labels = this.organizeArray(data.labels, permutation);
    }
    console.log(data);

  }

  organizeArray(originalArray: any[], permutation: number[]): any[] {
    const newArray = new Array(originalArray.length);
    for (let i = 0; i < originalArray.length; i++) {
      newArray[i] = originalArray[permutation[i]];
    }
    return newArray;
  }

  getChartData(): { labels: string[], datasets: any[] } {
    if(this.byCatagory) {
      return this.chartDataCategory;
    }
    return this.chartData;
  }

  selectTag(tagId: string) {
    this.chartData = this.transformData(false);
    this.chartDataCategory = this.transformData(true);
    this.tagSha1 = tagId;
    this.users = this.contributors(this.repository, this.tagSha1);
    let url = `${this.repository.id}/tag/${this.tagSha1}`
    this.router.navigateByUrl(url);
  }

  toggleGrouping(){
    this.byCatagory = !this.byCatagory;
  }



  transformData(byCategory: boolean): { labels: string[], datasets: any[] } {
    const labels: string[] = [];
    const datasets: any[] = [];
    const selectedTag = this.repository.tags[this.tagSha1];
    for (const contributor in selectedTag.contributions) {
      const contributions = selectedTag.contributions[contributor];
      if (!labels.includes(contributor)) {
        labels.push(contributor);
      }
      for (const type in contributions) {
        const tagContribution = contributions[type];
        if(byCategory){
          for(const subType in tagContribution){
            const value = tagContribution[subType];
            this.updateDatasets(subType, value, datasets);
          }
        } else {
          const value = Object.values(tagContribution).reduce((prev, curr) => prev + curr);
          this.updateDatasets(type, value, datasets);
        }
      }
    }
    return { labels, datasets };
  }

  updateDatasets(label: string, value: any, datasets: any[]) {
    const existingDatasetIndex = datasets.findIndex(dataset => dataset.label === label);
    if (existingDatasetIndex !== -1) {
      datasets[existingDatasetIndex].data.push(value);
    } else {
      datasets.push({
        label: label,
        data: [value],
        stack: 'stack',
      });
    }
  }

  contributors(repository: RepositoryDetails, tagSha1: string): string[] {
    return Array.from(Object.keys(this.repository.tags[tagSha1].contributions));
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.tagSha1 = params['tagSha1'];
      this.repositoryService.getRepositoryById(params['repoId'])
        .subscribe(res => {
          this.repository = res;
          if (!this.tagSha1) {
            this.tagSha1 = this.repository.tagsOrder[0];
          }
          this.selectTag(this.tagSha1);
        });
    });
  }

  contributionOf(user: string): { labels: string[], datasets: any[] }  {
    const contributions = this.repository.tags[this.tagSha1].contributions[user];
    const labels: string[] = ["CODE", "COMMENT", "BUILD"];
    const datasets: any[] = [];
    for (const type in contributions) {
      const tagContribution = contributions[type];
      let value = Array.from(Object.values(tagContribution)).reduce((pre, curr) => pre+curr);
      // Check if a dataset already exists for this type
      const existingDatasetIndex = datasets.findIndex(dataset => dataset.label === user);
      if (!labels.includes(type)) {
        labels.push(type);
      }
      if (existingDatasetIndex !== -1) {
        // If exists, add the value to the corresponding contributor
        datasets[existingDatasetIndex].data.push(value);
      } else {
        // If not, create a new dataset
        datasets.push({
          label: user,
          data: [value],
          stack: 'stack',
        });
      }
    }
    return { labels, datasets };
  }



}
