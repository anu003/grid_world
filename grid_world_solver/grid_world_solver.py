""" The MIT License (MIT)

    Copyright (c) 2014 Kyle Hollins Wray, University of Massachusetts

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this software and associated documentation files (the "Software"), to deal in
    the Software without restriction, including without limitation the rights to
    use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
    the Software, and to permit persons to whom the Software is furnished to do so,
    subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
    COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
    IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
"""

import sys
import csv
import numpy as np

# Load the file from standard input.
class GridWorldMDP(object):
    """ The GridWorldMDP class which loads a raw grid world file, and provides value iteration to solve it. """
    
    def __init__(self, filename):
        """ The constructor for the GridWorldMDP class, which loads a grid world MDP from a file.
        
            Parameters:
                filename    The grid world filename.
        """
        
        # Load all the data in this object.
        data = list()
        with open(filename, 'r') as f:
            reader = csv.reader(f, delimiter=',')
            for row in reader:
                data += [list(row)]
        
        # Attempt to parse all the data into their respective variables.
        try:
            self.n = int(data[0][0])
            self.m = 4
            self.R = [float(data[i + 1][0]) for i in range(self.n)]
            self.T = [[[float(data[(self.n + 1) + self.n * a + s][sp]) for sp in range(self.n)] for a in range(self.m)] for s in range(self.n)]
        except Exception:
            print("Failed to load file.")
            raise Exception()


    def solve(self, gamma=0.9, epsilon=0.01):
        """ Solve the GridWorldMDP using value iteration.
        
            Parameters:
                gamma      The discount factor on [0, 1].
                epsilon    The tolerance for convergence.
                
            Returns:
                V        The value of the states.
                pi       The policy of each of the states.
        """
        
        delta = 1.0 + epsilon
        
        self.V = [0.0 for i in range(self.n)]
        self.pi = [0 for i in range(self.n)]
        
        convergenceCriterion = epsilon
        if gamma < 1.0:
            convergenceCriterion *= (1.0 - gamma) / gamma
        
        while delta > convergenceCriterion:
            delta = 0.0
            for s in range(self.n):
                Qsa = [np.sum([self.T[s][a][sp] * self.V[sp] for sp in range(self.n)]) for a in range(self.m)]
                Vs = self.R[s] + gamma * np.max(Qsa)
                delta = max(delta, abs(Vs - self.V[s]))
                self.V[s] = Vs
                self.pi[s] = np.argmax(Qsa)
        
        return self.V, self.pi


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Please specify a file to load.")
    else:
        mdp  = GridWorldMDP(sys.argv[1])
        mdp.solve(1.0, 0.0001)
        print(mdp.V)
        print(mdp.pi)






