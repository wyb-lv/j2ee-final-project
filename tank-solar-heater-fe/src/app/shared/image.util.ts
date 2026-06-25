import { environment } from '../../environments/environment';

/** Backend origin (apiBase without the trailing /api) + the uploads path. */
const uploadsBase = environment.apiBase.replace(/\/api\/?$/, '') + '/uploads';

/**
 * Normalizes a product imageUrl into a usable <img src>.
 *
 * Product images are stored as a plain filename (e.g. "eco-tube.webp") and
 * served by the backend at {@code /uploads/<filename>}. Any leading directory
 * (e.g. legacy "/assets/images/...") is stripped so only the filename is used.
 * Absolute http(s) and data: URLs are passed through unchanged. Returns '' for
 * empty input so callers can fall back to their own placeholder.
 */
export function resolveImageUrl(url: string | null | undefined): string {
  const u = (url ?? '').trim();
  if (!u) return '';
  if (/^https?:\/\//i.test(u) || u.startsWith('data:')) return u;
  const filename = u.split('/').pop() ?? '';
  if (!filename) return '';
  return `${uploadsBase}/${filename}`;
}
