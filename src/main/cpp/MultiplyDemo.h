
#include <stdlib.h>
#include <stdio.h>

int multiply(int a, int b) {
    setbuf(stdout, NULL);
    printf("Multiplying %d * %d ...\n", a, b);
    return a * b;
}
