import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {LightRepository} from "./model/homePageModels";
import {RepositoryDetails} from "./model/repositoryDetails";

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  private api = "http://localhost:8080/api/repository/";

  constructor(private http: HttpClient) { }

  getRepositoryById(id: string): Observable<RepositoryDetails> {
    return this.http
      .get<RepositoryDetails>(`${this.api}${id}`);
  }

  getRepositories(): Observable<LightRepository[]> {
    return this.http
      .get<LightRepository[]>(`${this.api}`);
  }

  deleteRepository(id: string): Observable<any> {
    return this.http.delete(`${this.api}${id}`);
  }

  createRepository(url: string): Observable<LightRepository> {
    console.log({url: url});
    return this.http.post<LightRepository>(`${this.api}`, {url:url});
  }
}
