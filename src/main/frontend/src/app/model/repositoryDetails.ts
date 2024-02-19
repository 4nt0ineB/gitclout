


interface LineContribution {
  [subcategory: string]: number;
}

interface TagContribution {
  [category: string]: LineContribution;
}


export interface Tag {
  id: string;
  sha1: string;
  name: string[];
  contributions: {
    [contributor: string]: TagContribution;
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
