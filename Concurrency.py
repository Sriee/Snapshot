import sys
import traceback
import subprocess

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print "Wrong number of arguments."
        sys.exit(-1)

    numNodes = int(sys.argv[1])
    configFile = sys.argv[2]
    outList = {}
    result = True

    try:
        for n in range(0, numNodes):
            tk = int(1)
            dumList, vec = [], []

            print "Reading {0}".format(configFile + "-" + str(n) + ".out")
            line = subprocess.check_output(["tail", "-n", "3", (
                configFile + "-" + str(n) + ".out")])
            line = line.replace("\n", "$")
            line = line.strip()
            dumList = [i for i in line.split("$") if len(i) != 0]
            l = len(dumList)

            while not ('[' in dumList[l - tk] and ']' in dumList[l-tk]):
                tk += 1

            temp = dumList[l - tk]
            temp = temp.replace('[','')
            temp = temp.replace(']', '')
            temp = temp.replace(' ', '')
            vec = [int(token) for token in temp.split(',')]
            outList[n] = vec

        print(", ".join( repr(e) for e in outList.values()))
    except IOError:
        traceback.print_exc()

    print "Checking concurrent states."
    for i in range(0, numNodes):
        first = outList.get(i)
        for j in range(i, numNodes):
            less, great = False, False
            second = outList.get(j)
            if i == j:
                continue

            for k in range(0, numNodes):
                if first[k] < second[k]:
                    less = True
                if first[k] > second[k]:
                    great = True

                if less and great:
                    break

            if less and great:
                print "{0} and {1} are concurrent".format(i,j)
            else:
                result = False
                print "{0} and {1} are not concurrent".format(i, j)
    print "Result: {0}".format( "PASS" if result else "FAILED")