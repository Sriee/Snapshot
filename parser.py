import traceback
import sys


class Node:
    id, hostname = "", ""

    def __init__(self, id="", hostname=""):
        self.id = id
        self.hostname = hostname

    def get_id(self):
        return self.id

    def get_host_name(self):
        return self.hostname

    def __str__(self):
        return "id = {}, hostname = {}".format(self.id, self.hostname)


class Parser:
    CONFIG_FILE = "" # Add default config file name
    PROJ_NAME = "" # Add default project name
    number_of_nodes = 0
    net_id = "" # Add login credentials for the process
    nodes = None
    logger = None

    def __init__(self, logger = None):
        self.logger = logger
        self.logger.debug('Args: ' + ' '.join(sys.argv))
        if len(sys.argv) != 2:
            self.logger.debug("Wrong number of arguments.")
            sys.exit(-1)

        self.CONFIG_FILE = sys.argv[1]

    def parse_config_file(self):
        first_section = False
        section, node_stored = 0, 0
        try:
            cfg = open(self.CONFIG_FILE, "r")
            for input_line in cfg:

                if input_line.startswith("#"):
                    continue

                if '#' in input_line:
                    input_line = input_line[:input_line.find('#')]
                line = input_line.strip()

                if len(line) == 0:
                    continue

                # Extracting number of nodes and Net id
                if not first_section:
                    self.number_of_nodes = line.split(' ')[0].strip()
                    self.nodes = []
                    first_section = True
                    section += 1
                    self.logger.debug("Number of Nodes = %s", self.number_of_nodes)
                    continue

                if section == 1:
                    lst = line.split()
                    new_node = Node( lst[0], lst[1] )
                    self.nodes.append( new_node )
                    node_stored += 1
                    if node_stored == int(self.number_of_nodes):
                        section += 1

            for items in self.nodes:
                self.logger.debug( items )

            self.logger.debug("Closing %s", self.CONFIG_FILE)
            cfg.close()
            return self.nodes
        except IOError:
            self.logger.debug("Exception while opening \'%s\'", self.CONFIG_FILE)
            traceback.print_exc()
            sys.exit(-1)
        except ValueError:
            self.logger.debug("Exception in storing <node><host><port>")
            traceback.print_exc()
