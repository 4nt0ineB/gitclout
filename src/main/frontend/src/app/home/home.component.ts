import {Component, OnInit} from '@angular/core';
import {RepositoryService} from "../repository.service";
import {Repository} from "../model/homePageModels";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  repositories: Repository[] = [
    {
      "id": "e3d57419-3f4d-4deb-8c6b-cc130bfed638",
      "name": "asm",
      "url": "https://gitlab.ow2.org/asm/asm.git",
      "head": "bde266f0d59dd12739ad15a39f1da43a61143eed",
      "tags": {
        "8b56cad44ffb6809306ef3f0051b7495f8ae267b": {
          "name": [
            "v1",
            "v2"
          ],
          "parent": null
        },
        "8b56cad44ffb6809306ef3f0051b7495f8ae569a": {
          "name": [
            "v3",
            "v4"
          ],
          "parent": "8b56cad44ffb6809306ef3f0051b7495f8ae267b"
        }
      },
      "tagsOrder": [
        "8b56cad44ffb6809306ef3f0051b7495f8ae267b",
        "8b56cad44ffb6809306ef3f0051b7495f8ae569a"
      ]
    },
    {
      "id": "e3d57419-3f4d-4deb-8c6b-cc130bfed638",
      "name": "asm",
      "url": "https://gitlab.ow2.org/asm/asm.git",
      "head": "bde266f0d59dd12739ad15a39f1da43a61143eed",
      "tags": {
        "8b56cad44ffb6809306ef3f0051b7495f8ae267b": {
          "name": [
            "v1",
            "v2"
          ],
          "parent": null
        },
        "8b56cad44ffb6809306ef3f0051b7495f8ae569a": {
          "name": [
            "v3",
            "v4"
          ],
          "parent": "8b56cad44ffb6809306ef3f0051b7495f8ae267b"
        }
      },
      "tagsOrder": [
        "8b56cad44ffb6809306ef3f0051b7495f8ae267b",
        "8b56cad44ffb6809306ef3f0051b7495f8ae569a"
      ]
    }
  ];

  constructor(public repositoryService: RepositoryService, private route: ActivatedRoute) {
    this.repositoryService = repositoryService;
  }

  ngOnInit() {
    this.repositoryService.getRepositories().subscribe((res: Repository[]) => {
      console.log(res);
    })
  }
}
