import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LightRepository} from "../model/homePageModels";
import {from, Observable} from 'rxjs';
import { map } from 'rxjs/operators';
import {Utils} from "../utils";

@Component({
  selector: 'app-repository-item',
  templateUrl: './repository-item.component.html',
  styleUrls: ['./repository-item.component.css']
})
export class RepositoryItemComponent implements OnInit {

  ngOnInit(): void {}

  @Input()
  isOpen: boolean = false;

  @Input()
  repository!: LightRepository;

  @Output()
  deleteRepoEvent = new EventEmitter<LightRepository>();
  @Output()
  toggleEvent = new EventEmitter<string>();

  open() {
    this.isOpen = !this.isOpen;
    this.toggleEvent.emit(this.repository.id);
    console.log(this.repository);
  }

  progressionPercentage(): number {
    if(this.repository.analyzedTags === 0) {
      return 4;
    }
    return this.repository.analyzedTags * 100 / this.repository.totalTags;
  }

  progressBarColor() {
    let percentage = this.progressionPercentage();
    if (percentage >= 0 && percentage < 25) {
      return 'bg-red-500'; // Red for 0-24%
    } else if (percentage >= 25 && percentage < 50) {
      return 'bg-yellow-500'; // Yellow for 25-49%
    } else if (percentage >= 50 && percentage < 75) {
      return 'bg-orange-400'; // Blue for 50-74%
    } else {
      return 'bg-green-500'; // Green for 75-100%
    }
  }

  protected readonly Utils = Utils;
}
