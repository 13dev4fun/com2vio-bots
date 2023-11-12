export interface Record {
  id: number;
  owner: string;
  repo: string;
  pull: number;
  commit_id: string;
  file_path: string;
  line: string;
  start_line: number;
  end_line: number;
  type: string;
  detail: string;
  comment: string;
  relevant_final: number;
  relevant_1: number;
  relevant_2: number;
  visible?: boolean;
}

export interface RecordResponse {
  total: number;
  data: Record[];
}
