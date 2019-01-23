package akkadb.consistenthashing;

import com.google.common.hash.Hashing;

/**
 * @see Hashing.LinearCongruentialGenerator
 * @Date 上午1:08 2019/1/24
 * @Author: joker
 */
public class LCG {
	private long state;

	public LCG(long seed) {
		this.state = seed;
	}

	public double nextDouble() {
		state = 2862933555777941757L * state + 1;
		return ((double) ((int) (state >>> 33) + 1)) / 0x1.0p31;
	}

	public static void main(String[] args) {
		System.out.println(0x1.0p31);
		System.out.println(Math.pow(2.0, 31));
	}
}
