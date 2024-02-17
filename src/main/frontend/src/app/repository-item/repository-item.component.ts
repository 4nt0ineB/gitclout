import {Component, Input, OnInit} from '@angular/core';
import {LightRepository} from "../model/homePageModels";
import {from, Observable} from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-repository-item',
  templateUrl: './repository-item.component.html',
  styleUrls: ['./repository-item.component.css']
})
export class RepositoryItemComponent implements OnInit {
  isOpen: boolean = false;

  ngOnInit(): void {

  }

  @Input()
  repository!: LightRepository;


  open() {
    this.isOpen = !this.isOpen;
  }

  progressionPercentage(): number {
    return this.repository.analyzedTags * 100 / this.repository.totalTags;
  }

  progressBarColor() {
    let percentage = this.progressionPercentage();
    if (percentage >= 0 && percentage < 25) {
      return 'bg-red-500'; // Red for 0-24%
    } else if (percentage >= 25 && percentage < 50) {
      return 'bg-yellow-500'; // Yellow for 25-49%
    } else if (percentage >= 50 && percentage < 75) {
      return 'bg-blue-500'; // Blue for 50-74%
    } else {
      return 'bg-green-500'; // Green for 75-100%
    }
  }
}
