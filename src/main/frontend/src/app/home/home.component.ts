import {Component, OnInit} from '@angular/core';
import {RepositoryService} from "../repository.service";
import {LightRepository} from "../model/homePageModels";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  repositories: LightRepository[] = [
    {
      "id": "ae87ce16-37c6-429e-8bfb-3933490788b5",
      "username": "null",
      "name": "Calimba",
      "tagsOrder": ["e374d1ebfd71d9526e5d6e345762e48b0cb22f4e",
      "3f6809711c6418c5f5163900c40710178db65ccb",
      "aae650a9d5a9cbd69bf211f34bc4ced13d2ab9f8"
    ],
      "url": "https://github.com/SamueleGiraudo/Calimba.git",
      "tags": {
        "e374d1ebfd71d9526e5d6e345762e48b0cb22f4e": {
          "id": "003b5409-859c-45de-88a7-0257e1a434d4",
          "sha1": "e374d1ebfd71d9526e5d6e345762e48b0cb22f4e",
          "parentSha1": "3f6809711c6418c5f5163900c40710178db65ccb",
          "names": [
            "v0.1011"
          ]
        },
        "3f6809711c6418c5f5163900c40710178db65ccb": {
          "id": "a71bdb2c-1c62-4e86-a09e-a81ab7587474",
          "sha1": "3f6809711c6418c5f5163900c40710178db65ccb",
          "parentSha1": "aae650a9d5a9cbd69bf211f34bc4ced13d2ab9f8",
          "names": [
            "v0.1010"
          ]
        },
        "aae650a9d5a9cbd69bf211f34bc4ced13d2ab9f8": {
          "id": "0938364d-b06c-466b-be04-70de95a30d1b",
          "sha1": "aae650a9d5a9cbd69bf211f34bc4ced13d2ab9f8",
          "parentSha1": null,
          "names": [
            "v0.0011"
          ]
        }
      },
      "status": "done",
      "totalTags": 3,
      "analyzedTags": 3
    }
  ];

  constructor(public repositoryService: RepositoryService, private route: ActivatedRoute) {
    this.repositoryService = repositoryService;
  }

  ngOnInit() {
    this.repositoryService.getRepositories().subscribe((res: LightRepository[]) => {
      console.log(res);
    })
  }
}
