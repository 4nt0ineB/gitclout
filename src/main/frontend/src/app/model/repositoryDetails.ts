interface TagContribution {
  [category: string]: {
    [subcategory: string]: number
  };
}

interface Contributions {
  [username: string]: TagContribution;
}

export interface Tag {
  name: string[];
  contributions: {
    [contributor: string]: Contributions;
  };
  parent: string | null;
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
  tags: {
    [tagId: string]: Tag;
  };
  tagsOrder: string[];
}
