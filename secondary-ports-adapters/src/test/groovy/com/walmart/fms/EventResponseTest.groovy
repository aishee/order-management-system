package com.walmart.fms


import spock.lang.Specification

class EventResponseTest extends Specification {


}

class Order {
    String id;

    String getId() {
        return id
    }

    void setId(String id) {
        this.id = id
    }
}