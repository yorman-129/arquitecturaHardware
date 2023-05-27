public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }





    class Huffman {
        private static final int BUFFER_SIZE = 1024;

        public static void compress(String sourceFile, String compressedFile) {
            HashMap<Character, Integer> frequencyMap = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
                int c;
                while ((c = reader.read()) != -1) {
                    char character = (char) c;
                    frequencyMap.put(character, frequencyMap.getOrDefault(character, 0) + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();
            for (Character key : frequencyMap.keySet()) {
                HuffmanNode node = new HuffmanNode(frequencyMap.get(key), key);
                priorityQueue.add(node);
            }

            while (priorityQueue.size() > 1) {
                HuffmanNode left = priorityQueue.poll();
                HuffmanNode right = priorityQueue.poll();

                HuffmanNode newNode = new HuffmanNode(left.frequency + right.frequency, '-');
                newNode.left = left;
                newNode.right = right;

                priorityQueue.add(newNode);
            }

            HuffmanNode root = priorityQueue.poll();

            HashMap<Character, String> codeMap = new HashMap<>();
            generateCodes(root, "", codeMap);

            try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
                 BitOutputStream writer = new BitOutputStream(new FileOutputStream(compressedFile))) {
                int c;
                while ((c = reader.read()) != -1) {
                    char character = (char) c;
                    String code = codeMap.get(character);
                    for (char bit : code.toCharArray()) {
                        int value = bit == '0' ? 0 : 1;
                        writer.writeBit(value);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void generateCodes(HuffmanNode root, String code, HashMap<Character, String> codeMap) {
            if (root.left == null && root.right == null && Character.isLetter(root.data)) {
                codeMap.put(root.data, code);
                return;
            }
            generateCodes(root.left, code + "0", codeMap);
            generateCodes(root.right, code + "1", codeMap);
        }

        public static void decompress(String compressedFile, String decompressedFile) {
            try (BitInputStream reader = new BitInputStream(new FileInputStream(compressedFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(decompressedFile))) {

                HuffmanNode root = readTree(reader);
                HuffmanNode currentNode = root;

                int bit;
                while ((bit = reader.readBit()) != -1) {
                    if (bit == 0) {
                        currentNode = currentNode.left;
                    } else {
                        currentNode = currentNode.right;
                    }

                    if (currentNode.left == null && currentNode.right == null) {
                        writer.write(currentNode.data);
                        currentNode = root;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static HuffmanNode readTree(BitInputStream reader) throws IOException {
            int bit = reader.readBit();
            if (bit == 0) {
                return new HuffmanNode(-1, reader.readChar());
            }
            HuffmanNode left = readTree(reader);
            HuffmanNode right = readTree(reader);
            return new HuffmanNode(-1, '-');
        }

        public static void main(String[] args) {
            String sourceFile = "input.txt";
            String compressedFile = "compressed.bin";
            String decompressedFile = "decompressed.txt";

            compress(sourceFile, compressedFile);
            decompress(compressedFile, decompressedFile);

            System.out.println("Compression and decompression completed successfully.");
        }
    }

    class BitInputStream implements AutoCloseable {
        private FileInputStream inputStream;
        private int currentByte;
        private int currentBitIndex;

        public BitInputStream(FileInputStream inputStream) {
            this.inputStream = inputStream;
            this.currentByte = 0;
            this.currentBitIndex = 7;
        }

        public int readBit() throws IOException {
            if (currentBitIndex == -1) {
                currentByte = inputStream.read();
                if (currentByte == -1) {
                    return -1;
                }
                currentBitIndex = 7;
            }
            int bit = (currentByte >> currentBitIndex) & 1;
            currentBitIndex--;
            return bit;
        }

        public char readChar() throws IOException {
            int value = 0;
            for (int i = 0; i < 16; i++) {
                int bit = readBit();
                if (bit == -1) {
                    return (char) value;
                }
                value = (value << 1) | bit;
            }
            return (char) value;
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }

    class BitOutputStream implements AutoCloseable {
        private FileOutputStream outputStream;
        private int currentByte;
        private int currentBitIndex;

        public BitOutputStream(FileOutputStream outputStream) {
            this.outputStream = outputStream;
            this.currentByte = 0;
            this.currentBitIndex = 7;
        }

        public void writeBit(int bit) throws IOException {
            currentByte |= (bit & 1) << currentBitIndex;
            currentBitIndex--;
            if (currentBitIndex == -1) {
                outputStream.write(currentByte);
                currentByte = 0;
                currentBitIndex = 7;
            }
        }

        @Override
        public void close() throws IOException {
            if (currentBitIndex != 7) {
                outputStream.write(currentByte);
            }
            outputStream.close();
        }
    }

}