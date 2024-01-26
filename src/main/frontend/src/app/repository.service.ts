import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Repository} from "./model/homePageModels";

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  private api = "http://localhost:8080/api/repository/";

  constructor(private http: HttpClient) { }

  getRepositories(): Observable<Repository[]> {
    return this.http
      .get<Repository[]>(`${this.api}`);
  }
}
