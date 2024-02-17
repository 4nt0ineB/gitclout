
interface LightTag {
  parentSha1: string | null;
  names: string[];
  id: string;
  sha1: string;
}

export interface LightTags {
  [sha1: string]: LightTag;
}

export interface LightRepository {
  id: string;
  username: string;
  name: string;
  url: string;
  status: string;
  totalTags: number;
  analyzedTags: number;
  tags: LightTags;
  tagsOrder: string[];
}
