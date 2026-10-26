/** Create is its own page (not embedded on the index). Open via onOpen(CREATE_ID). */
export const CREATE_ID = 'new'

export function isCreatePage(detailId) {
  return detailId === CREATE_ID
}
