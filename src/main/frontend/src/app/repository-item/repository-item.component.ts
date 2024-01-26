import {Component, Input, OnInit} from '@angular/core';
import {Repository} from "../model/homePageModels";
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
  repository!: Repository;


  open() {
    this.isOpen = !this.isOpen;
  }
}
