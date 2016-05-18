# Project Part B - Playing a Game of Hexifence

### COMP30024 Artificial Intelligence - Semester 1 2016

#### Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>

Welcome to our submission. Here you will find a series of Java classes built
for playing the best game of Hexifence you have seen in your life! Our
solution is broken into a number of packages, and an even larger number of
classes (spanning over 4000 lines of code in total! In this comments.txt file,
we'll briefly discuss the structure of those packages and classes, before
diving into a report on our Agent's winning game strategy.

We've managed to develop an Agent that can, in a reasonable time and without
using much memory, search all the way to a terminal state from near the
beginning of a dimension 2 game. Obviously we're not enumerating all
possibilities in this search; we've used a host of domain-specific heuristics
and approximations to lower our branching factor. As a result, there are
several limitations to our Agent's strategy, which we have documented as well.
However, I think we can proudly declare that this Agent's Intelligence is a
product of our own, truly representing our mastery of the game (though with a
much faster clock speed!)

In addition to employing clever heuristics, our Agent employs two purpose-
built data structures on top of its board representation, for improving time
complexity where it counts. We've also been able to pursue the design of this
agent with more clarity than most, by leveraging LibGDX (a Java Game
Development Library) to render Hexifence Boards in colourful 2D -- so we can
watch our Agent dominate opponents without straining to interpret ASCII
symbols on the command line. We've included some sample .jar files so that you
can see this for yourself, and written more about it later in this report.

We hope you can take the time to enjoy reading about the fun we have had with 
this project.
                                                                Julian and Matt

Structure of Solution
=====================

Our solution became a bit expansive as we extended our simple agents and board
representation into its final form. It's made up of 8 packages; listed here in
rough order of relevance;

* unimelb.farrugiulian.hexifence.agent
    Here are our game-playing Agents:
    * Agent
        A superclass of all Hexifence Agents, legally playing a game 
        according to the Player interface, but delegating all 
        decision-making to its subclass' 'getChoice()' method. Made to 
        save on code duplication between Agents.
    * AgentFarrugiulian
        Our final Agent submission, more about its strategy later...
    * AgentGreedy
        An experimental AI playing the game with a greedy strategy - 
        making moves that capture cells if possible, otherwise minimising 
        the immediate gain of the opponent
    * AgentMe
        An experimental AI that forwards all of its decisions to a human 
        player by reading instructions from stdin
    * AgentRandom
        An experimental AI that selects random legal moves
    * DoubleAgent
        An experimental AI that plays the game with no searching, just 
        using a handful of hard-coded strategies to choose the best 
        visible move based on the information available

Our experimental Agents ended up helping us to build our final Agent, 
AgentFarrugiulian.

* unimelb.farrugiulian.hexifence.board
    This package contains our data structure implementing the game board, 
    made to abstract away logic related to navigating the game grid (
    instead considering it a collection of cells surrounded by edges, in a 
    more graph-like manner). See the javadoc for more information on these 
    classes and their provided functionality.

* unimelb.farrugiulian.hexifence.board.features
    This class provides another layer of abstraction above the game board, 
    allowing it to be viewed by the Agent as either a collection of 
    available Edges with certain classifications (based on the immediate 
    consequences of choosing those edges) and as a collection of 
    'Features', collections of edges that represent 'chains' or 'loops' of 
    one or more neighbouring cells that can all be captured if one of them 
    is captured; and 'intersections' where these features meet. We'll talk 
    more about these data structures later, too.

* com.matomatical.hexifence.visual
    Specialised Referee and Interfaces for interacting with the Hexifence 
    Visualiser (that we built using LibGDX)

* com.matomatical.util
    For extensions of standard Java utility classes, namely the 
    QueueHashSet extension of Java's LinkedHashSet (more about data 
    structures later)

* aiproj.hexifence
    Provided Referee class, Player interface, etc, unchanged since copied 
    over from dimefox on 16/05/16.

* aiproj.hexifence.matt
    Our own implementation of a Hexifence Referee, for experimental 
    purposes.

* unimelb.farrugiulian.hexifence.board.boardreading
    Some leftover classes adapted from Project Part A: for reading and 
    building boards based on specially-formatted ASCII input

AgentFarrugiulian
=================

Our Agent divides each game up into three stages - the opening stage, the mid-
game stage, and the endgame stage. We will outline the approaches used in each
stage, and the data structures used, below.

Opening Strategy
================

In the opening stages of the game, our agent plays a random, safe move (unless
there are cells available for capture)

As simple as it sounds, a deal of effort went into making these turns happen
as quickly as possible. It turns out creating a collection capable of
supporting constant time access and removal of specific edges, and also
constant time removal of a random edge, is not such a simple task.

The EdgeSet Data Structure
==========================

Our Agent achieves constant-time random moves by maintaining the first of our
specialised data structures - the EdgeSet. This data structure is also used by
the mid-game search algorithm, and the effects are more noticeable there,
where this complexity improvement is magnified by the exponential nature of a
search. Moreover, having a data structure supporting fast, randomised but
heuristic decision-making is a crucial part of an effective 'play-out' based
strategy, such as a Monte-Carlo tree search, or the strategy we ended up
arriving at for our final agent.

The data structure works by maintaining four fast-access collections of empty (
legal) Edges. The collections are:
    - free edges (capturing a cell)
    - capturing edges (capturing a cell and making another capture-able)
    - safe edges (don't make another cell capture-able)
    - sacrificing edges (make another cell capture-able)
When a move is played, the EdgeSet is updated and kept accurate, considering 
the consequences to all nearby Edges.

It's therefore important that we use a collection type that supports fast
insertion and removal of specific elements. HashSets come to mind. However,
our collections would be useless for our purposes if they did not support fast
removal of elements for selection. We need to be able to pull SOMETHING out of
the safe set, for example, when we are looking for a safe move to actually
play. HashSets, unfortunately, only support linear time removal in this
manner.

That's where the specialised utility class comes in. The QueueHashSet is an
extension of Java's native LinkedHashSet, which allows fast FIFO removal of
elements by maintaining a linked list atop the hash table structure. Our
QueueHashSet extends this to implement the Queue interface, providing
convenient methods for extracting an item as per our requirements. So with it,
we have the ability to insert, update, delete and get an element, all in O(1)
time! Awesome!

One further detail that helps speed up our game playing is the fact that the
EdgeSet supports the UN-making of moves. By keeping itself up to date in both
the forward and the backward directions, we are able to conduct searches on a
single underlying board (by restoring it to its original position as we unwind
the recursive search algorithms), saving us from having to copy the board to
generate new states - search transitions can happen in constant time!

Mid-game Strategy
=================

We wouldn't get very far with an agent that makes random moves. Our aim during
opening is actually to transition to the mid-game stage as early as possible,
such that our mid-game search algorithm will not take too long. We achieved
this by testing various transition thresholds and finding that when about 30
safe edges remain, our search completes in a number of seconds. If this turns
out to be too early, time-outs will ensure that we default back to effectively
random safe behaviour before a rouge search takes too much of our playtime.

Our approach to searching from this point is an approximate minimax search
with a highly simplified, but more effective variation of alpha-beta pruning
based on accurately determining the winning player from states with no
remaining safe edges.

Our search is 'approximate', as it does not actually consider ALL possible
moves from each search state. While there are still safe edges remaining, it
considers only these safe edges as valid moves. It's unfortunately possible,
however, that the only winning move requires making an early sacrifice of a
few cells, achieved by taking an edge that is NOT a safe edge. Our search
ignores these moves from both players' perspectives, and this means that our
Agent is blind to unsafe, winning moves. If they happen to exist in a
particular game, another Agent could defeat us even if we thought we were
going to win. However, these situations seem to be difficult to imagine and
rare to encounter, meaning that the time taken to consider them at every layer
of our search may not be worth the branching-factor blow-out!

Our Agent manages to complete searches beginning with between 20 and 30 safe
edges within a few seconds (with the time depending on the distribution of the
edges, depending on how many safe edges are eliminated by the placing of other
edges in the search). A branching factor of 30 may seem unrealistically high,
however in each ply the number of remaining safe moves is reduced by a number
larger than 1, as most safe moves involve the reclassification of about two
other moves as now unsafe. Therefore, the number of nodes in the search tree
is not 30!, but closer to 30*27*24*21*...*3 (a triple factorial:
https://en.wikipedia.org/wiki/Factorial#Double_factorial - slightly less than
the cube root of n!). Another result is that since the Agent's next turn is
likely to start with a board with two safe edges having been played and
therefore about 6 less safe edges, subsequent searches will have search trees
of rapidly decreasing size!

We have also included a simplified version of alpha-beta pruning in our mid-
game search. This relies on us being able to accurately evaluate boards with
no safe edges and determine the winning player. Since our resulting evaluation
function has a domain of only two possible values, we're able to prune more
aggressively; at each layer of the search tree, once we find a move that
results in the player at that layer winning the game, we can safely return
this as an optimal move without ever needing to evaluate any other options
from this state.

Our evaluation of boards with no safe edges is actually not a static
evaluation, but a fast 'play-out' search from that board all the way to a real
terminal state, using the same search strategy as our Agent uses during
endgame. Using the second of our specialised data structures (discussed
below), this search can be conducted with an incredibly low branching factor,
despite the fact that there are likely to be a large number of legal moves
remaining. This play-out is close to linear time in the size of the board in
practice. We can therefore quickly and accurately determine the expected
winner from one of these states, as if we were using a static evaluation
function, and drive the play towards these winning states.

The FeatureSet Data Structure
=============================

When there are no 'safe' moves remaining, it's useful to stop viewing the
board as a set of legal edges, and begin to view it as a set of higher-order
features; 'chains' and 'loops'. These are groups of cells that are bordered by
collections of edges, and choosing any one of the edges results in all of the
cells becoming available for capture one after the other.

At this point, when edges can be naturally grouped into sets of effectively
equivalent moves, a search over edges becomes pretty much useless due to its
unnecessarily high branching factor. It's much better to partition the edges
into their equivalence classes and search based on these.

This is where the FeatureSet comes in. A FeatureSet is a collection of these
board features (chains, loops, and the 'intersection' cells where they connect
to one another). Using a linear time algorithm reminiscent of Depth-First
Search, we can take a board and scan it for these Features. Then, we can
conduct a search by Cloning the FeatureSet and making changes to it, all the
while keeping track of the score on the board, before eventually reaching a
terminal state and inspecting this score to determine who the winner was.

Using the FeatureSet like this to dramatically reduce the branching factor in
the late stages of the game is the secret to our fast and accurate mid-game
evaluations. It's also the backbone of our endgame strategy, which we'll talk
more about soon. However, it has more potential than in just these
applications!

Firstly, having a way to carefully analyse the board for these features is a
fantastic first step to constructing a powerful static evaluation function;
where the existing features could be carefully analysed to determine a utility
value for a given non-terminal state. We basically decided not to go down this
route since any powerful utility function would need to take into account the
very complex interactions features and their counts as they play into the flow
of control at the late stages of the game, for example one important feature
can be the parity of the number of small sacrifices on the board as it
determines the player that will be forced to open the first long chain of
cells, likely losing the game if there are enough of these long features to
overcome the cost of the sacrifices beforehand.

Secondly, though not attempted for this project, a FeatureSet could be
extended to keep track of a board's features before there are no safe edges
remaining, in some sense 'merging' it with our EdgeSet to provide a richer
analysis of the current state of the board. Though incredibly complex to
implement (especially the requirement to keep it up to date with the placing
and removal of edges throughout the whole game) would make a more complete
mid-game and endgame search far more feasible.

Endgame Strategy
================

Our endgame strategy involves a small amount of narrow searching, followed by
a purely heuristic play-out (branching factor 1) driven by many hours of
careful consideration into optimal closing tactics.
    
Firstly, once again, we make any move that captures a single cell, without
making another cell capture-able. This is the only case where we can be
trivially sure that capturing is a good idea at this point of the game.

In contrast, when considering capturing cells that have consequences, we have
to think ahead. This new condition stems from the more common 'dots and boxes'
game, where in the endgame, taking these cells for immediate gain is not
always optimal. Instead, optimal play can involve making small sacrifices to
maintain control, forcing your opponent to sacrifice, or 'open', long chains
of boxes. To remain in control, you can 'double box' the last 2 boxes in a
chain (or the last 4 in a loop), offering them to the opponent, who has no
choice but to take them and open another chain for you. It turns out that
these strategies carry over nicely to Hexifence, the hexagonal variant of Dots
and Boxes.

If there are no moves that allow for this, we can still maybe capture cells
without thinking too much if we are not in a position to double box. So if we
are still able to capture cells, we calculate how many cells we can capture
before allowing the opponent to make their move. If this number is two, then
we know we are in a position where we might want to double box (if these two
cells were capture-able with the same move, then we would have made this move
before doing this check). Likewise, if this number is four and one of the
moves we would make in capturing these four cells would capture two cells at
once, then we are also in a position where we might want to double box.

Therefore, if we know we are in one of these position where we definitely
don't want to double box, then we can safely make moves that increase our
score without needing to consider how the game will turn out. We only need to
make sure that the edge we take does not remove the possibility that we are
able to double box later on (though the only time we should encounter this
situation is when the opponent makes a very bad move!). If we are in a
position to double box, we need to determine whether it is actually a good
idea to do so! Sometimes the sacrifices involved can outweigh the benefits
that come with control, especially if there are not many long features.

At this point, our strategy is to create a board state where it is as if we
chose to double box (so the opponent has captured the cells we gave up and it
is now their turn to move again), and then use our narrow search on features
followed by a heuristic play-out to play out the game from here, and check if
we are guaranteed to win. If so, then obviously double boxing is the right
choice (though this results in our agent sometimes being quite disrespectful
by double boxing as its very last move, effectively giving the opponent some
score because our agent does not need it). If not, then we choose to just
increase our own score.
	

The second possible situation we may encounter is if there are no capturing
moves available. If there are no moves that increase our score, then we need
to make a move that will allow the opponent to increase their score (making a
sacrifice).

Again, we use a search on features followed by a heuristic play-out to
determine if we can be guaranteed a win from here. If so, then we make the
sacrifice returned by the feature search, and if not we make the smallest
sacrifice possible. How we make this sacrifice is important if we are making a
sacrifice of two cells. If we are 'in control' of the game, then we do not
want to allow the opponent to double box our sacrifice of size 2! This would
take away our control. So we sacrifice the chain securely by choosing the edge
in between the two cells. If we are not in control, we sacrifice the chain on
the edge, hoping that the opponent is not smart and double boxes it, so that
we may subsequently take control, or at the very least increase our score a
little.

We've mentioned our search over features a few times. This search is our way
of taking advantage of the FeatureSet's reduced branching factor. A short and
narrow search is used to consider all of the various ways of breaking up the
connected features (which can influence the flow of control past this point).
However, once all features are isolated, we can search with a branching factor
of one, and guarantee that if the opposing makes any moves other than what we
are considering, we will not do any worse. Usually, the small number of
intersected features that can be taken before all features are isolated
results in a very quick search, especially on boards of dimension 2 or 3.
Therefore, though theoretically expensive, this search-based evaluation
completes very quickly in practice.

Other Creative Aspects: The Hexifence Visualisation Engine
==========================================================

In addition to the classes that make up our submission, as we have mentioned
we also created a visualisation engine using LibGDX. We have not included the
source code for this visualisation engine, but have packaged a few standalone
.jar files so that you can view the results. Time delays have been included
between moves so that the games are easier to follow. See the included .jar
files in our submission, and have fun watching our agents play off on boards
of various dimensions!

    usage: java -jar <jarname>.jar
        jarname - 6_AgentFarrugiulian_AgentGreedy
                    watch our agent (blue) play against a greedy agent (red) 
                    on a dimension 6 board
                - 3_AgentFarrugiulian_AgentGreedy
                    watch our agent (blue) play against a greedy agent (red) 
                    on a dimension 3 board
                - 3_AgentFarrugiulian_AgentFarrugiulian
                    watch our agent (blue) play against itself (red) on a 
                    dimension 3 board
    
OR

    Double click <jarname>.jar in a file explorer

Each time the jars are run, the agents are seeded with the System nanotime.
This means you can get unlimited fun, watching them over and over again!

Other Creative Aspects: Clever Ideas that Didn't Quite Work, and Why
====================================================================

While developing the mid-search over safe edges, we came to the realisation
that the order in which safe edges are played makes no difference to the
result of those edges being played; by nature of their definitions, safe edges
do not capture any cells and therefore they do not change the game score, only
the board layout. Safe edges may rule our other safe edges, but they do so in
a symmetrical way in that the order that edges are played does not change
which edges can be combined while all remaining safe.

As a result, there is actually a lot of unnecessary re-evaluation going on
during our safe edge search. Each path through the search tree consisting of n
safe edges will be generated n! times - once for each permutation of the order
of edges in that path - yet each time, the same evaluation will be performed!
What a waste!

One way to 'prune' these repeat evaluations is to somehow generate paths in
the search tree in a canonical pattern, such that no equivalent paths are
generated twice. We implemented this pruning mechanism by generating moves
with edges only lexicographically greater (based on their i, j position on the
board grid) than those the edges in the path so far, so that each eventual
distinct path is only generated once: in sorted order. Brilliant! This reduced
the size of our search significantly; if a search beginning from n possible
safe edges, and (as discussed earlier) each edge taken removes itself and an
average of 2 other edges from the remaining count, the size of the search tree
is O(n!!!) (triple factorial - (n)(n-3)(n-6)...(6)(3) - approximated by
(n!)^(1/3)) without accounting for win/lose pruning. This means the search
tree has a depth of roughly n/3. As a result, each path is roughly n/3 states
long, and therefore has (n/3)! permutations, all occurring somewhere in the
search tree. Only evaluating each of these permutations once therefore reduces
the number of evaluations by a factor of (n/3)! - an amazing improvement.

There's only one problem: this pruning strategy doesn't actually work! It's
due to the nature of an adversarial search. The problem is that we have no way
to re-evaluate paths at different points in the tree - for example if a
minimising player at some node has a winning strategy that uses an out-of-
order edge, and no other winning strategies, then EVEN if we eventually or
previously evaluate this combination of edges to find out this fact, at the
moment in the tree this minimising player will think they are our of options
and may report a loss to the maximising player above, who will incorrectly see
that as a win, and so forth. It's not enough to only evaluate each combination
of edges only once; we also have to be able to retrieve these evaluations
multiple times during a minimax search.

One solution to this problem that would still improve our search performance is to store the evaluations of our pseudo-terminal states (with no remaining safe edges) and also nodes above them in the search tree in a 'transposition table'. We could populate the table whenever we encounter a combination for the first time, caching its result after running our evaluation search. Or, we could use a lexicographically-pruned state exploration search to generate all game states in advance (possibly not ideal overall, since win/lose pruning may render some of these searches unnecessary eventually anyway). The idea of using a transposition table to aid our mid-game search has a lot of merit. Even within the restrictive memory constraints on our submission, we could have potentially employed a caching strategy to use as much memory as possible during our searches.