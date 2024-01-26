interface Contributions {
  [typeContribution: string]: { [contribution: string]: number };
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
  name: string;
  url: string;
  head: string;
  tags: {
    [tagId: string]: Tag;
  };
  tagsOrder: string[];
}

