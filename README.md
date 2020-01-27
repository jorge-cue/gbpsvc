# GBPSVC - Get best Price Service

This exercise is to compare 4 different strategies to find the best price among a set of stores for a given product
specified by an SKU, it is supposed that stores are identified by a fourth digit number.

For testing purposes, stores are emulated using WireMock located on localhost port 8085.

The strategies to compare are:

Number|Strategy name|Description|
-----:|:------------|:----------|
   1  | synchronous | Each store is queried synchronously queries are stopped waiting for current store before launching next one |
   2  | synchronous parallel | All stores are queried in parallel, using streams, number of queries in parallel depend on stream library implementation |
   3  | asynchronous | Queries are submitted asynchronous and result is controlled using a CompletableFuture |
   4  | asynchronous custom executor | Same as 3 but using a custom thread pool. |
   
Then tests are running for 1, 10, 20, 30, 40, 50, 100, 200, 300, 400, 500 and 1000 stores, then time spent is in the next table.
Please note sin it is statistic figures may differ for you.

Stores|synchronous|parallel|asynchronous|custom executor|
-----:|----------:|-------:|-----------:|--------------:|
 1    | 2 s 47 ms | 1 s 308 ms | 200 ms | 225 ms | 
 10    | 6 s 883 ms | 1 s 463 ms | 1 s 795 ms | 3 s 322 ms | 
 20    | 11 s 967 ms | 5 s 41 ms | 2 s 661 ms | 3 s 662 ms | 
 30    | 17 s 586 ms | 5 s 583 ms | 5 s 697 ms | 5 s 693 ms | 
 40    | 27 s 6 ms | 6 s 663 ms | 7 s 82 ms | 6 s 495 ms | 
 50    | 32 s 589 ms | 8 s 564 ms | 10 s 76 ms | 7 s 148 ms | 
 100    | 1 m 20 s 567 ms | 15 s 827 ms | 18 s 557 ms | 13 s 350 ms | 
 200    | 2 m 26 s 520 ms | 32 s 583 ms | 30 s 833 ms | 30 s 569 ms | 
 300    | 3 m 57 s 111 ms | 51 s 99 ms | 47 s 184 ms | 42 s 107 ms | 
 400    | 4m 50 s 361 ms | 59 s 152 ms | 1 m 1 s 723 ms | 1 m 3 s 382 ms | 
 500    | 6 m 10 s 389 ms | 1 m 19 s 987 ms | 1 m 13 s 477 ms | 1 m 16 s 671 ms | 
 1000    | 12 m 8 s 846 ms | 2 m 31 s 808 ms | 2 m 30 s 968 ms | 2 m 42 s 344 ms | 

`Running on an Intel(R) Core(TM) i7-8750H 8 cores CPU @ 2.2 GHz, 16 GB.` 
