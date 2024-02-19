import {Component, HostListener, signal} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {RepositoryDetails, Tag} from "../model/repositoryDetails";
import {RepositoryService} from "../repository.service";
import {Utils} from "../utils";
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

  chartData: { labels: string[], datasets: any[] } = {
    labels: [],
    datasets: []
  }

  chartDataCategory : { labels: string[], datasets: any[] } = {
    labels: [],
    datasets: []
  }

  constructor(private route: ActivatedRoute, private router: Router, private repositoryService: RepositoryService) { }

  onWheel(event: WheelEvent) {
    event.preventDefault(); // Prevent default vertical scrolling
    const delta = Math.max(-1, Math.min(1, (event.deltaY || -event.detail)));
    const container = event.currentTarget as HTMLElement;
    container.scrollLeft += delta * 60; // Adjust scroll speed as needed
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

  transformData(byCatagory: boolean): { labels: string[], datasets: any[] } {
    const labels: string[] = [];
    const datasets: any[] = [];

    // Retrieve contributions for the selected tag
    const selectedTag = this.repository.tags[this.tagSha1];

    // Iterate through each contributor's contributions for the selected tag
    for (const contributor in selectedTag.contributions) {
      const contributions = selectedTag.contributions[contributor];

      // Check if the contributor is already in labels, if not, add it
      if (!labels.includes(contributor)) {
        labels.push(contributor);
      }

      // Create a dataset for each type of contribution
      for (const type in contributions) {
        const tagContribution = contributions[type];
        if(byCatagory){
          for(const subType in tagContribution){
            let value = tagContribution[subType];

            // Check if a dataset already exists for this type
            const existingDatasetIndex = datasets.findIndex(dataset => dataset.label === subType);
            if (existingDatasetIndex !== -1) {
              // If exists, add the value to the corresponding contributor
              datasets[existingDatasetIndex].data.push(value);
            } else {
              // If not, create a new dataset
              datasets.push({
                label: subType,
                data: [value],
                stack: 'stack',
              });
            }
          }

        }else{
          let value = Array.from(Object.values(tagContribution)).reduce((pre, curr) => pre+curr);
          // Check if a dataset already exists for this type
          const existingDatasetIndex = datasets.findIndex(dataset => dataset.label === type);
          if (existingDatasetIndex !== -1) {
            // If exists, add the value to the corresponding contributor
            datasets[existingDatasetIndex].data.push(value);
          } else {
            // If not, create a new dataset
            datasets.push({
              label: type,
              data: [value],
              stack: 'stack',
            });
          }
        }
      }
    }
   return { labels, datasets };
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
