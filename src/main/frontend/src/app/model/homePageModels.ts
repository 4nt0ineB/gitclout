interface Tag {
  parent: string | null;
  name: string[];
}

export interface Tags {
  [tagId: string]: Tag;
}

export interface Repository {
  id: string;
  name: string;
  url: string;
  head: string;
  tags: Tags;
  tagsOrder: string[];
}

export interface ProgressionStatus {
  analyzedTags: number;
  id: string;
  repositoryName: string;
  status: string;
  totalTags: number;
  url: string;
}

