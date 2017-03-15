package at.archistar.crypto;

import at.archistar.crypto.data.Share;
import at.archistar.crypto.decode.DecoderFactory;
import at.archistar.crypto.decode.ErasureDecoderFactory;
import at.archistar.crypto.random.BCDigestRandomSource;
import at.archistar.crypto.random.RandomSource;
import at.archistar.crypto.secretsharing.KrawczykCSS;
import at.archistar.crypto.secretsharing.ReconstructionException;
import at.archistar.crypto.secretsharing.SecretSharing;
import at.archistar.crypto.secretsharing.ShamirPSS;
import at.archistar.crypto.secretsharing.WeakSecurityException;
import at.archistar.crypto.symmetric.ChaCha20Encryptor;

/**
 * This is a simple CryptoEngine that allows us to use ITS secret-sharing scheme.
 *
 * @author Andreas Happe <andreashappe@snikt.net>
 */
public class KrawczykEngine implements CryptoEngine {

    /** our ITS Shamir secret-sharing scheme */
    private final SecretSharing shamir;

    /** how many shares should be generated */
    private final int n;

    /** minimum amount of shares needed to reconstruct original data */
    private final int k;

    /**
     * initialize the crypto engine
     *
     * @param n total number of shares
     * @param k minimum count of shares needed to recreate the original data
     * @throws WeakSecurityException if the k/n selection is insecure
     */
    public KrawczykEngine(int n, int k) throws WeakSecurityException {
        this(n, k, new BCDigestRandomSource());
    }

    /**
     * Create a new Shamir Engine.
     *
     * @param n total number of shares
     * @param k minimum count of shares needed to recreate the original data
     * @param rng random number generator to be used
     * @throws WeakSecurityException if the k/n selection is insecure
     */
    public KrawczykEngine(int n, int k, RandomSource rng) throws WeakSecurityException {
        DecoderFactory decoderFactory = new ErasureDecoderFactory();

        this.shamir = new KrawczykCSS(n, k, rng, new ChaCha20Encryptor(), decoderFactory);
        this.n = n;
        this.k = k;
    }

    @Override
    public Share[] share(byte[] data) {
        return this.shamir.share(data);
    }

    @Override
    public byte[] reconstruct(Share[] shares) throws ReconstructionException {
        return this.shamir.reconstruct(shares);
    }

    @Override
    public byte[] reconstructPartial(Share[] shares, long start) throws ReconstructionException {
        for (Share s : shares) {
            s.setInformationChecking(Share.ICType.NONE);
        }
        return this.shamir.reconstructPartial(shares, start);
    }

    @Override
    public String toString() {
        return "Krawcyk(" + k + "/" + n + ")";
    }
}
