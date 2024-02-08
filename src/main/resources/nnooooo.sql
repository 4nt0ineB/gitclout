CREATE TABLE IF NOT EXISTS git_repository (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    url VARCHAR(500),
    path VARCHAR(500),
    head VARCHAR(41) -- sha1
    );

CREATE TABLE IF NOT EXISTS tag (
     id VARCHAR(41) PRIMARY KEY, -- sha1
     commit_time INT NOT NULL,
     parent_id VARCHAR(41),
     repository_id UUID NOT NULL,
     name VARCHAR(255) NOT NULL,
     FOREIGN KEY (repository_id) REFERENCES git_repository(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tag_contribution (
    id UUID UNIQUE,
    contributor VARCHAR(41),
    tag_id VARCHAR(41) NOT NULL,
    PRIMARY KEY (id, contributor, tag_id),
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contribution_detail (
    tag_contribution_id UUID NOT NULL,
    contribution_type VARCHAR(255) NOT NULL,
    nb_lines INT NOT NULL,
    FOREIGN KEY (tag_contribution_id) REFERENCES tag_contribution(id) ON DELETE CASCADE,
    PRIMARY KEY (tag_contribution_id, contribution_type)
);

