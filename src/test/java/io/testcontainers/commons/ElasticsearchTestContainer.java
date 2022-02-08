package io.testcontainers.commons;

import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class ElasticsearchTestContainer extends ElasticsearchContainer {
    private static final String DOCKER_ELASTIC = "docker.elastic.co/elasticsearch/elasticsearch:7.11.2";

    private static final String CLUSTER_NAME = "local-cluster";

    private static final String ELASTIC_SEARCH = "elasticsearch";

    public ElasticsearchTestContainer() {
        super(DOCKER_ELASTIC);
        this.addFixedExposedPort(9200, 9200);
        this.addFixedExposedPort(9300, 9300);
        this.addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
    }
}
