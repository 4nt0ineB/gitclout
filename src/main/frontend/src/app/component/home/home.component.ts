import {Component, OnInit} from '@angular/core';
import {RepositoryService} from "../../service/repository.service";
import {LightRepository} from "../../model/homePageModels";
import {ActivatedRoute} from "@angular/router";
import {interval, take} from "rxjs";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  urlForm: FormGroup;

  repositoriesStates: {[repositoryId: string]: boolean} = {};
  repositories: LightRepository[] = [];

  constructor(public repositoryService: RepositoryService, private route: ActivatedRoute, private formBuilder: FormBuilder) {
    this.repositoryService = repositoryService;
    this.urlForm = this.formBuilder.group({
      url: ['', [Validators.required, Validators.pattern('^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$')]]
    });
  }

  addRepo(): void {
    if(this.urlForm.invalid) {
      return;
    }
    this.repositoryService.createRepository(this.urlForm.get("url")?.value)
      .subscribe({
          complete: console.info,
          error: err => {
            console.info(err);
          }
        });
  }

  deleteRepo(repoIndex: number) {
    console.log("delete " + repoIndex);
    this.repositoryService.deleteRepository(this.repositories[repoIndex].id)
      .subscribe({
        complete: console.info,
      });
  }

  toggleItem(id: number) {
    let repoId = this.repositories[id].id;
    this.repositoriesStates[repoId] = !this.repositoriesStates[repoId];
  }

  ngOnInit() {
    this.repositoryService.getRepositories()
      .subscribe((res: LightRepository[]) => {
        this.repositories = res;
        this.repositories.forEach((item) => {
          if (this.repositoriesStates[item.id] === undefined) {
            this.repositoriesStates[item.id] = false; // Default state (e.g., closed)
          }
        });
      });
    const interval$ = interval(2000);
    interval$.pipe().subscribe(() => {
      this.repositoryService.getRepositories()
        .subscribe((res: LightRepository[]) => {
          this.repositories = res;
          this.repositories.forEach((item) => {
            if (this.repositoriesStates[item.id] === undefined) {
              this.repositoriesStates[item.id] = false; // Default state (e.g., closed)
            }
          });
        });
    });
  }
}
