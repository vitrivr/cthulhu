package org.vitrivr.cthulhu.runners;

class MainCoordinator {
    public static void main(String[] args) {
        CoordinatorRunner cr = new CoordinatorRunner();
        cr.start(args);
    }
}
