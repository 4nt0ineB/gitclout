interface TagContribution {
  [category: string]: {
    [subcategory: string]: number
  };
}

interface Contributions {
  [username: string]: TagContribution;
}

export interface Tag {
  id: string;
  sha1: string;
  name: string[];
  contributions: {
    [contributor: string]: Contributions;
  };
  parent: string | null;
}

export interface Tags {
  [tagId: string]: Tag;
}

export interface RepositoryDetails {
  id: string;
  username: string;
  name: string;
  url: string;
  status: string;
  totalTags: number;
  head: string;
  analyzedTags: number
  tags: Tags;
  tagsOrder: string[];
}
