#!/bin/bash
# Set number of nodes to use and CPUs per node
#PBS -lnodes=1:ppn=1
# Set wall time
#PBS -lwalltime=0:03:00
# Redirect standard erro stream
#PBS -e 
# Redirect standard output stream
#PBS -o 
# cd to the work directory
cd $HOME/omnimatch
# Load appropriate modules
module load openmpi/gnu/64/1.4.2
# Determine the number of processors:
nprocs=`wc -l < $PBS_NODEFILE`
echo Starting OMNIMATCH in directory $PBS_O_WORKDIR
echo The $nprocs  allocated nodes are:
cat $PBS_NODEFILE
# run the program
echo Start now... 
exit 0
