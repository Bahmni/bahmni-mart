package org.bahmni.mart.executors;

class MartExecutionOrder {

    /**
     * Lower value means higher precedence.
     */
    static final int JOB = 0;
    static final int PROCEDURE = 1;
    static final int VIEW = 2;

}
